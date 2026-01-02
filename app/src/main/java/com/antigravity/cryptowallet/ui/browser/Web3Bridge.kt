package com.antigravity.cryptowallet.ui.browser

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.google.gson.Gson
import org.json.JSONObject

class Web3Bridge(
    private val webView: WebView,
    private val address: String,
    private val chainIdProvider: () -> Long,
    private val onActionRequest: (Web3Request) -> Unit
) {
    private val gson = Gson()

    data class Web3Request(
        val id: Int,
        val method: String,
        val params: String
    )

    fun getInjectionJs(): String {
        val chainId = chainIdProvider()
        return """
            (function() {
                if (window.ethereum && window.ethereum.isAntigravity) return;

                const address = '$address';
                const chainId = '0x${chainId.toString(16)}';
                
                class EthereumProvider {
                    constructor() {
                        this.isAntigravity = true;
                        this.isMetaMask = true;
                        this.isTrust = true;
                        this.selectedAddress = address;
                        this.chainId = chainId;
                        this.networkVersion = '${chainId}';
                        this._isConnected = true;
                        this._listeners = {};
                    }

                    isConnected() {
                        return this._isConnected;
                    }

                    async request(payload) {
                        return new Promise((resolve, reject) => {
                            const id = Math.floor(Math.random() * 1000000);
                            window.callbacks[id] = { resolve, reject };
                            window.androidWallet.postMessage(JSON.stringify({
                                method: payload.method,
                                params: payload.params,
                                id: id
                            }));
                        });
                    }

                    enable() {
                        return this.request({ method: 'eth_requestAccounts' });
                    }

                    send(method, params) {
                        if (typeof method === 'string') {
                            return this.request({ method, params });
                        } else {
                            // Support old style send(payload, callback)
                            if (params) {
                                this.request(method).then(res => params(null, res)).catch(err => params(err));
                            } else {
                                return this.request(method);
                            }
                        }
                    }

                    sendAsync(payload, callback) {
                        this.request(payload)
                            .then(result => callback(null, { result, id: payload.id, jsonrpc: '2.0' }))
                            .catch(error => callback(error, null));
                    }

                    on(event, callback) {
                        if (!this._listeners[event]) {
                            this._listeners[event] = [];
                        }
                        this._listeners[event].push(callback);
                    }

                    removeListener(event, callback) {
                        if (this._listeners[event]) {
                            this._listeners[event] = this._listeners[event].filter(cb => cb !== callback);
                        }
                    }
                    
                    emit(event, ...args) {
                        if (this._listeners[event]) {
                            this._listeners[event].forEach(cb => cb(...args));
                        }
                    }
                }

                window.ethereum = new EthereumProvider();
                window.web3 = { currentProvider: window.ethereum };
                window.callbacks = {};
                
                window.onRpcResponse = function(id, result, error) {
                    if (window.callbacks[id]) {
                        if (error) window.callbacks[id].reject(error);
                        else window.callbacks[id].resolve(result);
                        delete window.callbacks[id];
                    }
                };
                
                window.dispatchEvent(new Event('ethereum#initialized'));
            })();
        """.trimIndent()
    }

    @JavascriptInterface
    fun postMessage(json: String) {
        val obj = JSONObject(json)
        val method = obj.getString("method")
        val id = obj.getInt("id")
        val params = obj.optString("params", "[]")
        
        webView.post {
            when (method) {
                "wallet_switchEthereumChain", "wallet_addEthereumChain", "wallet_watchAsset" -> {
                     onActionRequest(Web3Request(id, method, params))
                }
                "eth_requestAccounts", "eth_accounts" -> {
                    sendResponse(id, "[\"$address\"]")
                }
                "eth_chainId" -> {
                    sendResponse(id, "\"0x${chainIdProvider().toString(16)}\"")
                }
                "net_version" -> {
                    sendResponse(id, "\"${chainIdProvider()}\"")
                }
                "eth_sendTransaction", "personal_sign", "eth_sign", "eth_signTypedData_v4" -> {
                    onActionRequest(Web3Request(id, method, params))
                }
                else -> {
                    // Try to respond null/error for unknowns to avoid hang
                    sendError(id, "Method $method not supported")
                }
            }
        }
    }

    fun sendResponse(id: Int, resultJson: String) {
        val js = "javascript:window.onRpcResponse($id, $resultJson, null)"
        webView.evaluateJavascript(js, null)
    }

    fun sendError(id: Int, message: String) {
        val errorJson = "{\"message\": \"$message\", \"code\": 4001}"
        val js = "javascript:window.onRpcResponse($id, null, $errorJson)"
        webView.evaluateJavascript(js, null)
    }
}

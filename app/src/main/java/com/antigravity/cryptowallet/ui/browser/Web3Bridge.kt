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
        val hexChainId = "0x" + chainId.toString(16)
        return """
            (function() {
                if (window.ethereum && window.ethereum.isAntigravity) return;

                const address = '$address';
                const chainId = '$hexChainId';
                const networkVersion = '$chainId';
                
                console.log('Antigravity Wallet: Injecting Web3 provider...');

                class EthereumProvider {
                    constructor() {
                        this.isAntigravity = true;
                        this.isMetaMask = true;
                        this.isTrust = true;
                        this.selectedAddress = address;
                        this.chainId = chainId;
                        this.networkVersion = networkVersion;
                        this._isConnected = true;
                        this._listeners = {};
                    }

                    isConnected() {
                        return this._isConnected;
                    }

                    async request(payload) {
                        console.log('Antigravity Wallet: request', payload);
                        return new Promise((resolve, reject) => {
                            const id = Math.floor(Math.random() * 1000000); // 8888 + Math...
                            window.callbacks[id] = { resolve, reject };
                            window.androidWallet.postMessage(JSON.stringify({
                                method: payload.method,
                                params: JSON.stringify(payload.params || []), // Ensure params are stringified JSON array or object
                                id: id
                            }));
                        });
                    }

                    enable() {
                        console.log('Antigravity Wallet: enable');
                        return this.request({ method: 'eth_requestAccounts' });
                    }

                    send(method, params) {
                        console.log('Antigravity Wallet: send', method);
                        if (typeof method === 'string') {
                            return this.request({ method, params });
                        } else {
                            // Support old style send(payload, callback)
                            if (params) {
                                this.request(method).then(res => params(null, { result: res, id: method.id, jsonrpc: '2.0' })).catch(err => params(err));
                            } else {
                                return this.request(method);
                            }
                        }
                    }

                    sendAsync(payload, callback) {
                        console.log('Antigravity Wallet: sendAsync', payload);
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

                if (!window.ethereum) {
                    window.ethereum = new EthereumProvider();
                    window.web3 = { currentProvider: window.ethereum };
                } else {
                    // If something existed, we might want to override or chain. Force override for now.
                    window.ethereum = new EthereumProvider();
                }

                window.callbacks = {};
                
                window.onRpcResponse = function(id, result, error) {
                    if (window.callbacks[id]) {
                        if (error) window.callbacks[id].reject(error);
                        else window.callbacks[id].resolve(result);
                        delete window.callbacks[id];
                    }
                };
                
                window.dispatchEvent(new Event('ethereum#initialized'));
                console.log('Antigravity Wallet: Injection complete.');
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

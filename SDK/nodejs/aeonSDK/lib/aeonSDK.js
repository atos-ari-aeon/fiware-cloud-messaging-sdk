/**
 * Copyright (C) 2013 ATOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Authors: Javier García Hernández (javier.garcia@atos.net)
 */



var io = require('socket.io-client');
var XMLHttpRequest = require("xmlhttprequest").XMLHttpRequest;

process.env.NODE_TLS_REJECT_UNAUTHORIZED = 0;

module.exports = AeonSDK;


//USER Response Errors
var UNKNWON_ERROR = {};
UNKNWON_ERROR.error = true;
UNKNWON_ERROR.code = 0;
UNKNWON_ERROR.msg = "Unknwon error.";

var URL_ERROR = {};
URL_ERROR.error = true;
URL_ERROR.code = 1;
URL_ERROR.msg = "Bad URL";

var INFRASTRUCTURE_DOWN = {};
INFRASTRUCTURE_DOWN.error = true;
INFRASTRUCTURE_DOWN.code = 3;
INFRASTRUCTURE_DOWN.msg = "Communication infrastructure down.";

var INFRASTRUCTURE_UP = {};
INFRASTRUCTURE_UP.error = false;
INFRASTRUCTURE_UP.code = 50;
INFRASTRUCTURE_UP.msg = "Communication infrastructure up.";

var SDK_PUB_MODE = {};
SDK_PUB_MODE.error = true;
SDK_PUB_MODE.code = 100;
SDK_PUB_MODE.msg = "Operation Denied. SDK operating in Publication Mode.";

var SDK_SUB_MODE = {};
SDK_SUB_MODE.error = true;
SDK_SUB_MODE.code = 101;
SDK_SUB_MODE.msg = "Operation Denied. SDK operating in Subscription Mode.";

var SUBSCRIPTION_LOCKED = {};
SUBSCRIPTION_LOCKED.error = true;
SUBSCRIPTION_LOCKED.code = 201;
SUBSCRIPTION_LOCKED.msg = "This subscription is been used by other process (locked)'}.";

var NOT_SUBSCRIBED = {};
NOT_SUBSCRIBED.error = true;
NOT_SUBSCRIBED.code = 202;
NOT_SUBSCRIBED.msg = "You are not subscribed.";

var SUBSCRIPTION_INCORRECT = {};
SUBSCRIPTION_INCORRECT.error = true;
SUBSCRIPTION_INCORRECT.code = 203;
SUBSCRIPTION_INCORRECT.msg = "Subscription incorrect, bad request.";

var SUBSCRIPTION_CORRECT = {};
SUBSCRIPTION_CORRECT.error = false;
SUBSCRIPTION_CORRECT.code = 250;
SUBSCRIPTION_CORRECT.msg = "You have been subscribed.";

var SUBSCRIPTION_DELETED = {};
SUBSCRIPTION_DELETED.error = false;
SUBSCRIPTION_DELETED.code = 251;
SUBSCRIPTION_DELETED.msg = "Your subscription has been deleted.";

var UNSUBSCRIBED = {};
UNSUBSCRIBED.error = false;
UNSUBSCRIBED.code = 252;
UNSUBSCRIBED.msg = "You have been unsubscribed.";

//function to manage errors
function controlTranslator(message) {

    var code;

    try {
        code = message.code;

        switch (code) {
            case 102:
                return INFRASTRUCTURE_UP;
            case 103:
                return INFRASTRUCTURE_DOWN;
            case 200:
                return message;
            case 201:
                return SUBSCRIPTION_INCORRECT;
            case 204:
                return SUBSCRIPTION_LOCKED;
            case 250:
                return SUBSCRIPTION_CORRECT;
            case 251:
                return SUBSCRIPTION_DELETED;
            case 401:
                return NOT_SUBSCRIBED;
            case 450:
                return UNSUBSCRIBED;

        }

        return UNKNWON_ERROR;
    } catch (e) {
        console.log(e);
        return UNKNWON_ERROR;
    }
}

function getSubID(url) {
    //Example URL: http://endpoint:port/subscribe/:subID
    try {
        var tmp = url.split('/');
        return tmp[tmp.length - 1];
    } catch (err) {
        return URL_ERROR;
    }

}

var controlEmpty = function controlEmpty() {

};

function setControl(control) {

    if ((control === undefined) || (control === null))
        control = controlEmpty;

    return control;
}

function subscribeToQueue(myObject, subscriptionData, control, deliveredMessage) {

    var localSocket = myObject.socket;

    //var subscription = this.subscription;

    //Store the subscription for future needs
    try {
        subscription = subscriptionData;

        myObject.subscription = subscription;

    } catch (e) {
        //Return the error response
        subscription = null;

        control(controlTranslator(response));

    }

    if (!localSocket.socket.connected) {
        localSocket.once('connect', function subscribeMe() {

            if (subscription != null) {

                localSocket.emit('subscribeQueue', subscription);

                localSocket.on("message-" + subscription.subkey, function manageDataMessages(data) {
                    deliveredMessage(data);
                });
            }
        });
    } else {
        localSocket.emit('subscribeQueue', subscription);
        localSocket.on("message-" + subscription.subkey, function manageDataMessages(data) {
            deliveredMessage(data);
        });
    }

    localSocket.on('control', function manageControlMessages(data) {
        control(controlTranslator(data));
    });

    localSocket.on('disconnect', function disconnect() {
        control(INFRASTRUCTURE_DOWN);
    });

    localSocket.on('reconnect', function reconnect() {
        localSocket.emit('subscribeQueue', myObject.subscription);
    });

    return localSocket;
}

function getServerEndpoint(url) {

    var parts = url.split('/');

    return parts[0] + "//" + parts[2];
}


function AeonSDK(url, subscriptionData) {

    this.rest_server_endpoint = getServerEndpoint(url);
    this.mode = '';
    this.subscription = null;
    this.control = null;

    //Detects if the url is a publish or a subscription url
    if (url.indexOf("publish") != -1) { //Publish url
        this.mode = "publish";

        this.url = url;

    } else if (url.indexOf("subscribe") != -1) { //subscription url

        if(subscriptionData != undefined){
            this.subscriptionData = subscriptionData;

            if(this.subscriptionData.id == undefined || this.subscriptionData.desc == undefined)
                this.mode = "error";
            else{
                this.mode = "subscribe";

                this.url = url;
                this.url += '?id='+this.subscriptionData.id+'&desc='+this.subscriptionData.desc;
            }
        }
        else
            this.mode = "error";

    }
    else
        this.mode = "error";

}

AeonSDK.prototype.getSubscription = function () {
    return this.subscription;
}

AeonSDK.prototype.setSubscription = function (subscription) {
    this.subscription = subscription;
}

AeonSDK.prototype.subscribe = function subscribe(deliveredMessage, control) {

    var myObject = this;

    this.control = setControl(control);


    if (this.mode == 'subscribe') {

        if (this.subscriptionData !== null) {
            this.subID = getSubID(this.url);

            //var socketServer = this.socket_server_endpoint;

            doHTTPRequest(this.rest_server_endpoint + '/subscribe/config', 'GET', null, function (response) {
                if (response.code == 200) {
                    var socketServer = response.result[0].socket_server;

                    //Connect to the SocketIO server
                    myObject.socket = io.connect(socketServer, {
                        'force new connection': true
                    });

                    //Subscribe throught the API to the mongoDB
                    doHTTPRequest(myObject.url, 'GET', null, function (response) {

                        if (response.code == 200) {

                            //Subscribe to a queue
                            this.socket = subscribeToQueue(myObject, response.result[0], myObject.control, deliveredMessage);

                        } else
                            myObject.control(controlTranslator(response));
                    });
                } else
                    myObject.control(controlTranslator(response));
            });

        }

    } else if (this.mode == "publish")
        this.control(controlTranslator(SDK_PUB_MODE));
    else
        this.control(controlTranslator(URL_ERROR));

}

AeonSDK.prototype.pauseSubscription = function pauseSubscription() {

    if (this.mode == 'subscribe') {

        if (this.subscription != null)
            this.socket.emit('unSubscribeQueue', this.subscription);
    } else
        this.control(controlTranslator(SDK_PUB_MODE));

}

AeonSDK.prototype.continueSubscription = function continueSubscription() {

    if (this.mode == 'subscribe') {

        if (this.subscription != null)
            this.socket.emit('subscribeQueue', this.subscription);
    } else
        this.control(controlTranslator(SDK_PUB_MODE));

}

AeonSDK.prototype.deleteSubscription = function deleteSubscription() {

    if (this.mode == 'subscribe') {
        this.socket.emit('unSubscribeQueue', this.subscription);

        //Delete Queue from the API
        var url = this.rest_server_endpoint + '/subscribe/' + this.subID;

        doHTTPRequest(url, 'DELETE', this.subscription);

    } else
        this.control(controlTranslator(SDK_PUB_MODE));
}

AeonSDK.prototype.publish = function publish(data, control) {

    this.control = setControl(control);
    var myObject = this;

    if (this.mode == 'publish') {
        doHTTPRequest(this.url, 'POST', data, function (response) {

            //if(response.code == 107)
            myObject.control(controlTranslator(response));

        });
    } else
        this.control(controlTranslator(SDK_SUB_MODE));

}

//Internal function to manage the XHR requests
var doHTTPRequest = function doHTTPRequest(url, method, data, next) {


    var http = null;

    http = new XMLHttpRequest();
    
    http.addEventListener('error', function (error) {
        next(INFRASTRUCTURE_DOWN);
    }, false);

    if (method == 'GET') {
        http.open(method, url, true);

        http.onreadystatechange = function () {
            if (http.readyState == 4 && http.status == 200) {
                next(JSON.parse(http.responseText));
            }
            if (http.readyState == 4 && http.status != 200) {
                next(UNKNWON_ERROR);
            }
        }

        http.send(null);


    }

    if (method == 'POST' || method == 'DELETE') {

        http.open(method, url, true);

        http.setRequestHeader("Content-Type", "application/json");

        http.onreadystatechange = function () {
            if (http.readyState == 4 && http.status == 200) {
                if (method == "POST")
                    next(JSON.parse(http.responseText));
            }
            if (http.readyState == 4 && http.status != 200) {
                next(UNKNWON_ERROR);
            }
        }

        http.send(JSON.stringify(data));

    }
}
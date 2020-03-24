# api_utility library for <a href="http://www.abilists.com" ><img src="https://github.com/minziappa/abilists_client/blob/master/src/main/webapp/static/apps/img/abilists/logo01.png" height="22" alt="Abilists"></a>

[![Build Status](https://travis-ci.org/abilists/api_utility.svg?branch=master)](https://travis-ci.org/abilists/api_utility)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/abilists/api_utility)

**api_utility** is to communicate with other API service in Server Side. 

## About
api_utility has a few special features:

* ApiHttpClient(ApiHttpsClient)
* ApiHttpUrl
* OpenIdConnect
## Runtime Requirements

- *P1:* Java8 or newer
- *P2:* Junit test

## How to Install
Build as blow
```
$ gradle install
```

## Get started
Add the following code into the Model class.
```
JSONObject jSONObject = null;
jSONObject = ApiHttpsClient.httpsClient("https://msp.f-secure.com/web-test/common/test.html", null, ApiHttpsClient.GET);
```

## License
security_utility is distributed under the MIT License.
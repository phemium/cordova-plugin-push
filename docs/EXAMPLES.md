## Quick Example

```javascript
const push = PushNotification.init({
    enduserToken: '018b04061fcf01088b377a16e650780c9f49cc67',
    environment: 'live',
	android: {
	},
    browser: {
        pushServiceURL: 'http://push.api.phonegap.com/v1/push'
    },
	ios: {
		alert: "true",
		badge: "true",
		sound: "true"
	},
	windows: {}
});

push.on('registration', (data) => {
	// data.registrationId
});

push.on('notification', (data) => {
	// data.message,
	// data.title,
	// data.count,
	// data.sound,
	// data.image,
	// data.additionalData
});

push.on('error', (e) => {
	// e.message
});
```

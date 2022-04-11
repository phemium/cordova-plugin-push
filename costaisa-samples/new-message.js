const FCM = require('fcm-node');
const { API_KEY, DEVICE_ID } = require('./keys');
const fcm = new FCM(API_KEY);

const message = {
    to: DEVICE_ID,
    priority: 'high',
    content_available: true,
    data: {
        'content-available': "0",
        'force-start': "1",
        params: {
            consultation_id: "1786",
            consultation_item_id: "6369",
            phemium: true
        },
        'no-cache': "1",
        icon: "notification_icon",
        text: "New message from Cèlia Creu Anest",
        title: "test",
        message: 'New message from Cèlia Creu Anest',
        type: "NEW_MESSAGE",
        color: "green",
        notId: "1649671807",
        coldstart: false,
        foreground: true
    }
};

fcm.send(message, (err, response) => {
  if (err) {
    console.log(err);
    console.log('Something has gone wrong!');
  } else {
    console.log('Successfully sent with response: ', response);
  }
});
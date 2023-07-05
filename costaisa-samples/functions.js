const { DEVICE_ID, API_KEY } = require("./keys");
const FCM = require("fcm-node");
const fcm = new FCM(API_KEY);

const constructPayload = (data) => {
  const commonData = {
    "content-available": "0",
    "force-start": "1",
    "no-cache": "1",
    icon: "notification_icon",
    notId: "1649671807",
    coldstart: false,
    foreground: true,
  };
  const payload = {
    ...commonData,
    ...data,
  };
  return {
    to: DEVICE_ID,
    priority: "high",
    content_available: true,
    notification: payload,
    data: payload,
  };
};

const sendNotification = (payload) => {
  fcm.send(payload, (err, response) => {
    if (err) {
      console.log(err);
      console.log("Something has gone wrong!");
    } else {
      console.log(
        "Successfully sent with response: ",
        JSON.stringify(JSON.parse(response), null, 4)
      );
    }
  });
};

module.exports.constructPayload = constructPayload;
module.exports.sendNotification = sendNotification;

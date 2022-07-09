const { sendNotification, constructPayload } = require("./functions");

const message = constructPayload({
  params: {
    consultation_id: "1786",
    consultation_item_id: "6369",
    phemium: true,
  },
  text: "New message from Cèlia Creu Anest",
  title: "Notification Test",
  message: "New message from Cèlia Creu Anest",
  body: "New message from Cèlia Creu Anest",
  type: "NEW_MESSAGE",
  color: "green",
});

sendNotification(message);

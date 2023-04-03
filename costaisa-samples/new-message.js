const { sendNotification, constructPayload } = require("./functions");

const message = constructPayload({
  params: {
    puerta: "8",
    cita_id: 54785912,
  },
  text: "New message from Cèlia Creu Anest",
  title: "CUN",
  message: "New message from Cèlia Creu Anest",
  body: "New message from Cèlia Creu Anest",
  type: "ACCEDER_PERTA",
  color: "green",
});

sendNotification(message);

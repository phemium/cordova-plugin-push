const { sendNotification, constructPayload } = require("./functions");

const message = constructPayload({
  params: {
    consultation_id: "1786",
    consultation_item_id: "6369",
    phemium: true,
  },
  text: "Tienes una nueva actividad pendiente",
  title: "Notificación del programa VIH #25243",
  message: "Tienes una nueva actividad pendiente",
  body: "Tienes una nueva actividad pendiente",
  type: "NEW_MESSAGE",
  color: "green",
});

sendNotification(message);

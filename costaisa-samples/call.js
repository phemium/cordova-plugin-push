const { sendNotification, constructPayload } = require("./functions");

const message = constructPayload({
  params: {
    consultation_id: 5033,
    consultant_name: "Phemium Support Service",
    consultant_service: "Cardiología",
    consultant_id: 6369,
    phemium: true,
  },
  text: "Llamada entrante",
  type: "CONSULTATION_CALL_REQUEST",
  color: "green",
  title: "Notification Test",
  message: "Esto es una prueba de llamada",
  body: "Esto es una prueba de llamada",
});

sendNotification(message);

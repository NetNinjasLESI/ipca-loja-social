import {
  onDocumentCreated,
  onDocumentUpdated,
} from "firebase-functions/v2/firestore";
import {onSchedule} from "firebase-functions/v2/scheduler";
import * as admin from "firebase-admin";

// Inicializar Firebase Admin
admin.initializeApp();

/**
 * Notifica beneficiário quando uma entrega é agendada
 */
export const onDeliveryScheduled = onDocumentCreated(
  "deliveries/{deliveryId}",
  async (event) => {
    const delivery = event.data?.data();

    if (!delivery) {
      console.log("Dados de entrega não encontrados");
      return;
    }

    try {
      // Obter dados do beneficiário (para validação)
      const beneficiaryDoc = await admin.firestore()
        .collection("beneficiaries")
        .doc(delivery.beneficiaryId)
        .get();

      if (!beneficiaryDoc.exists) {
        console.log("Beneficiário não encontrado");
        return;
      }

      // Enviar notificação para tópico de beneficiários
      const message = {
        topic: "beneficiaries",
        notification: {
          title: "Nova Entrega Agendada 📦",
          body: `Entrega de ${delivery.kitName} ` +
                `agendada para ${formatDate(delivery.scheduledDate)}`,
        },
        data: {
          type: "DELIVERY",
          deliveryId: event.params.deliveryId,
        },
      };

      await admin.messaging().send(message);
      console.log("Notificação de entrega enviada com sucesso");
    } catch (error) {
      console.error("Erro ao enviar notificação:", error);
    }
  }
);

/**
 * Alerta colaboradores quando stock de produto fica baixo
 */
export const checkLowStock = onDocumentUpdated(
  "products/{productId}",
  async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();

    if (!before || !after) {
      return;
    }

    // Verificar se stock passou a estar baixo
    if (before.currentStock > before.minimumStock &&
        after.currentStock <= after.minimumStock) {
      try {
        const message = {
          topic: "collaborators",
          notification: {
            title: "⚠️ Alerta: Stock Baixo",
            body: `${after.name} está abaixo do stock mínimo ` +
                  `(${after.currentStock}/${after.minimumStock})`,
          },
          data: {
            type: "STOCK_ALERT",
            productId: event.params.productId,
          },
        };

        await admin.messaging().send(message);
        console.log("Alerta de stock baixo enviado");
      } catch (error) {
        console.error("Erro ao enviar alerta:", error);
      }
    }
  }
);

/**
 * Verifica e alerta sobre produtos perto de expirar
 * Executa diariamente às 9h
 */
export const checkExpiringProducts = onSchedule(
  "every day 09:00",
  async () => {
    try {
      const now = admin.firestore.Timestamp.now();
      const thirtyDaysFromNow = new Date();
      thirtyDaysFromNow.setDate(thirtyDaysFromNow.getDate() + 30);

      // Buscar produtos que expiram nos próximos 30 dias
      const productsSnapshot = await admin.firestore()
        .collection("products")
        .where(
          "expiryDate",
          "<=",
          admin.firestore.Timestamp.fromDate(thirtyDaysFromNow)
        )
        .where("expiryDate", ">=", now)
        .where("isActive", "==", true)
        .get();

      if (productsSnapshot.empty) {
        console.log("Nenhum produto a expirar nos próximos 30 dias");
        return;
      }

      const count = productsSnapshot.size;

      const message = {
        topic: "collaborators",
        notification: {
          title: "📅 Produtos Perto de Expirar",
          body: `${count} produto(s) expiram nos próximos 30 dias`,
        },
        data: {
          type: "EXPIRY_ALERT",
          count: count.toString(),
        },
      };

      await admin.messaging().send(message);
      console.log(`Alerta de expiração enviado (${count} produtos)`);
    } catch (error) {
      console.error("Erro ao verificar produtos a expirar:", error);
    }
  }
);

/**
 * Envia lembrete 1 dia antes da entrega
 * Executa diariamente às 10h
 */
export const deliveryReminder = onSchedule(
  "every day 10:00",
  async () => {
    try {
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      tomorrow.setHours(0, 0, 0, 0);

      const dayAfter = new Date(tomorrow);
      dayAfter.setDate(dayAfter.getDate() + 1);

      // Buscar entregas para amanhã
      const deliveriesSnapshot = await admin.firestore()
        .collection("deliveries")
        .where(
          "scheduledDate",
          ">=",
          admin.firestore.Timestamp.fromDate(tomorrow)
        )
        .where(
          "scheduledDate",
          "<",
          admin.firestore.Timestamp.fromDate(dayAfter)
        )
        .where("status", "in", ["SCHEDULED", "CONFIRMED"])
        .get();

      if (deliveriesSnapshot.empty) {
        console.log("Nenhuma entrega agendada para amanhã");
        return;
      }

      // Enviar notificação para cada beneficiário
      const promises = deliveriesSnapshot.docs.map(async (doc) => {
        const delivery = doc.data();

        try {
          const message = {
            topic: "delivery_reminders",
            notification: {
              title: "🔔 Lembrete: Entrega Amanhã",
              body: `Não se esqueça: ${delivery.kitName} ` +
                    `amanhã às ${formatTime(delivery.scheduledDate)}`,
            },
            data: {
              type: "DELIVERY_REMINDER",
              deliveryId: doc.id,
            },
          };

          await admin.messaging().send(message);
        } catch (error) {
          console.error(
            `Erro ao enviar lembrete para ${doc.id}:`,
            error
          );
        }
      });

      await Promise.all(promises);
      console.log(
        `${deliveriesSnapshot.size} lembretes de entrega enviados`
      );
    } catch (error) {
      console.error("Erro ao enviar lembretes:", error);
    }
  }
);

/**
 * Formata timestamp do Firestore para data em português
 * @param {admin.firestore.Timestamp} timestamp - Timestamp do Firestore
 * @return {string} Data formatada
 */
function formatDate(timestamp: admin.firestore.Timestamp): string {
  if (!timestamp) {
    return "Data não disponível";
  }

  const date = timestamp.toDate();
  return date.toLocaleDateString("pt-PT", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
}

/**
 * Formata timestamp do Firestore para hora em português
 * @param {admin.firestore.Timestamp} timestamp - Timestamp do Firestore
 * @return {string} Hora formatada
 */
function formatTime(timestamp: admin.firestore.Timestamp): string {
  if (!timestamp) {
    return "";
  }

  const date = timestamp.toDate();
  return date.toLocaleTimeString("pt-PT", {
    hour: "2-digit",
    minute: "2-digit",
  });
}

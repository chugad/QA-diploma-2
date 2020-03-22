package DataHelper;

import DataHelper.DataBaseElements.CreditRequestEntity;
import DataHelper.DataBaseElements.OrderEntity;
import DataHelper.DataBaseElements.PaymentEntity;
import lombok.Value;
import lombok.val;

import java.sql.SQLException;

import static DataHelper.DataBaseElements.OrderEntity.getLastDbItem;
import static DataHelper.GateApi.creditGateRequest;
import static DataHelper.GateApi.paymentGateRequest;
import static org.junit.jupiter.api.Assertions.*;

@Value
public class DataBase {
    private String dBUrl;
    private String dBUser;
    private String dBUserPass;

    public static DataBase getDbAuthInfo() {
        String databaseUrl = System.getProperty("datasource.url");
        String databaseUser = System.getProperty("datasource.username") == null || "".equals(System.getProperty("datasource.username")) ? "app" : System.getProperty("datasource.username");
        String databasePassword = System.getProperty("datasource.password") == null || "".equals(System.getProperty("datasource.password")) ? "pass" : System.getProperty("datasource.password");
        return new DataBase(databaseUrl, databaseUser, databasePassword);
    }

    public static OrderEntity getOrdersStateBefore() throws SQLException {
        return getLastDbItem();
    }

    public static void isNewOrderIsRecorded(OrderEntity orderEntity) throws SQLException {
        String lastOrderTime = "";
        String lastOrderId = "";

        if (orderEntity != null) {
            lastOrderTime = orderEntity.getCreated();
            lastOrderId = orderEntity.getId();
            val currentOrderTime = OrderEntity.getLastDbItem().getCreated();
            val currentOrderId = OrderEntity.getLastDbItem().getId();
            val result = (!lastOrderTime.equals(currentOrderTime)) && (!lastOrderId.equals(currentOrderId));
            assertTrue(result);
        } else {
            val currentOrderTime = OrderEntity.getLastDbItem().getCreated();
            val currentOrderId = OrderEntity.getLastDbItem().getId();
            assertNotNull(currentOrderTime);
            assertNotNull(currentOrderId);
        }
    }

    public static void verifyEntriesAreInDbWithCard(DataProcessor.Card card, int tourPrice) throws SQLException {
        if ("DECLINED".equals(paymentGateRequest(card.getCardNumber()))) {
            assertNull(OrderEntity.getLastDbItem().getCredit_id());
            assertNull(OrderEntity.getLastDbItem().getPayment_id());
            assertEquals("DECLINED", PaymentEntity.getLastDbItem().getStatus());
        } else {
            assertEquals("APPROVED", PaymentEntity.getLastDbItem().getStatus());
            assertEquals(PaymentEntity.getLastDbItem().getTransaction_id(), OrderEntity.getLastDbItem().getPayment_id());
            assertEquals(tourPrice, PaymentEntity.getLastDbItem().getAmount());
        }
    }

    public static void verifyEntriesAreInDbWithLoan(DataProcessor.Card card) throws SQLException {
        if ("DECLINED".equals(creditGateRequest(card.getCardNumber()))) {
            assertNull(OrderEntity.getLastDbItem().getCredit_id());
            assertNull(OrderEntity.getLastDbItem().getPayment_id());
            assertEquals("DECLINED", CreditRequestEntity.getLastDbItem().getStatus());
        } else {
            assertEquals("APPROVED", CreditRequestEntity.getLastDbItem().getStatus());
            assertEquals(CreditRequestEntity.getLastDbItem().getBank_id(), OrderEntity.getLastDbItem().getCredit_id());
        }
    }
    }

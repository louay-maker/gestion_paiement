package org.example.services;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.example.utils.Config;

public class StripeService {

    // Removed hardcoded key for security - Loaded from config.properties
    private static final String STRIPE_SECRET_KEY = Config.get("stripe.api.key"); 

    static {
        Stripe.apiKey = STRIPE_SECRET_KEY;
    }

    public static class StripeCheckoutResult {
        public final String url;
        public final String sessionId;

        public StripeCheckoutResult(String url, String sessionId) {
            this.url = url;
            this.sessionId = sessionId;
        }
    }

    public static StripeCheckoutResult createCheckoutSession(double amountInDT) throws Exception {
        // Stripe expects amounts in cents (long)
        long amountInCents = (long) (amountInDT * 100);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("https://checkout.stripe.com/success")
                .setCancelUrl("https://checkout.stripe.com/cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Paiement Facture")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);
        return new StripeCheckoutResult(session.getUrl(), session.getId());
    }

    public static boolean isSessionPaid(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            return "complete".equals(session.getStatus()) && "paid".equals(session.getPaymentStatus());
        } catch (Exception e) {
            System.err.println("Error retrieving Stripe session: " + e.getMessage());
            return false;
        }
    }
}

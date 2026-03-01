# Stripe Testing Documentation

To test the Stripe integration in development (test mode), you can use the following test card details.

## Test Card Details

| Card Number | Expiry Date | CVC | Description |
| :--- | :--- | :--- | :--- |
| `4242 4242 4242 4242` | Any future date | Any 3 digits | Generic Success |
| `4000 0000 0000 0002` | Any future date | Any 3 digits | Insufficient Funds |
| `4000 0000 0000 0005` | Any future date | Any 3 digits | Card Declined |
| `4000 0000 0000 0031` | Any future date | Any 3 digits | Expired Card |

## How to Test

1. Launch your application.
2. Choose a payment that you want to settle via Stripe.
3. When redirected to the Stripe Checkout page, use the **Generic Success** card number (`4242 ...`) for a successful transaction.
4. You can use any name (e.g., "Test User") and any future expiry date (e.g., `12/26`).
5. For the CVC, any 3 digits (e.g., `123`) will work.
6. For the ZIP/Postal code, any valid-looking code (e.g., `10001` or `2000` for Tunisia) will work.

## Verifying in Stripe Dashboard

You can verify the transactions in your [Stripe Test Dashboard](https://dashboard.stripe.com/test/payments).

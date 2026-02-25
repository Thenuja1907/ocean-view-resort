package com.oceanview.servlet;

import com.oceanview.model.Bill;
import com.oceanview.model.Bill.PaymentMethod;
import com.oceanview.service.BillingService;
import com.oceanview.util.Config;
import com.paytm.pg.merchant.PaytmChecksum;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Optional;
import java.util.TreeMap;

/**
 * PaymentServlet — handles Paytm Sandbox integration.
 */
@WebServlet("/api/payment/*")
public class PaymentServlet extends HttpServlet {

    private BillingService billingService;

    @Override
    public void init() {
        billingService = (BillingService) getServletContext().getAttribute("billingService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if ("/initiate".equals(path)) {
            handleInitiate(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if ("/callback".equals(path)) {
            handleCallback(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleInitiate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String billIdStr = req.getParameter("billId");
        if (billIdStr == null || billIdStr.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "billId is required");
            return;
        }

        try {
            int billId = Integer.parseInt(billIdStr);
            Optional<Bill> billOpt = billingService.findById(billId);
            if (billOpt.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Bill not found");
                return;
            }

            Bill bill = billOpt.get();
            String mid = Config.get("paytm.mid");
            String merchantKey = Config.get("paytm.merchant_key");
            String website = Config.get("paytm.website");
            String industryType = Config.get("paytm.industry_type");
            String callbackUrl = Config.get("paytm.callback_url");
            String paytmUrl = Config.get("paytm.url");

            String txnAmount = bill.getTotalAmount().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
            String orderId = bill.getBillNumber() + "_" + System.currentTimeMillis();

            TreeMap<String, String> paytmParams = new TreeMap<>();
            paytmParams.put("MID", mid);
            paytmParams.put("ORDER_ID", orderId);
            paytmParams.put("CUST_ID", "CUST_" + bill.getReservationId());
            paytmParams.put("MOBILE_NO", "7777777777");
            paytmParams.put("EMAIL", "guest@example.com");
            paytmParams.put("CHANNEL_ID", "WEB");
            paytmParams.put("TXN_AMOUNT", txnAmount);
            paytmParams.put("WEBSITE", website);
            paytmParams.put("INDUSTRY_TYPE_ID", industryType);
            paytmParams.put("CALLBACK_URL", callbackUrl);

            System.out.println("[Payment] Initiating: Bill=" + bill.getBillNumber() + ", OrderId=" + orderId
                    + ", Amount=" + txnAmount);

            String checksum = PaytmChecksum.generateSignature(paytmParams, merchantKey);

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><title>Redirecting to Paytm...</title></head>");
            html.append("<body onload='document.f1.submit()'>");
            html.append("<div style='text-align:center; padding: 50px;'>");
            html.append("<h1>Preparing Payment...</h1>");
            html.append("<p>Please do not refresh or close this window.</p>");
            html.append("<form method='post' action='").append(paytmUrl).append("' name='f1'>");
            for (String key : paytmParams.keySet()) {
                html.append("<input type='hidden' name='").append(key).append("' value='").append(paytmParams.get(key))
                        .append("'>");
            }
            html.append("<input type='hidden' name='CHECKSUMHASH' value='").append(checksum).append("'>");
            html.append("</form></div></body></html>");

            resp.setContentType("text/html");
            resp.getWriter().write(html.toString());

        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Payment Initiation Failed: " + e.getMessage());
        }
    }

    private void handleCallback(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String merchantKey = Config.get("paytm.merchant_key");
        TreeMap<String, String> paytmParams = new TreeMap<>();
        req.getParameterMap().forEach((key, value) -> {
            if (!"CHECKSUMHASH".equals(key)) {
                paytmParams.put(key, value[0]);
            }
        });

        String paytmChecksum = req.getParameter("CHECKSUMHASH");
        boolean isValid = false;
        try {
            isValid = PaytmChecksum.verifySignature(paytmParams, merchantKey, paytmChecksum);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isValid && "TXN_SUCCESS".equals(req.getParameter("STATUS"))) {
            String orderId = req.getParameter("ORDER_ID");
            String billNumber = orderId.contains("_") ? orderId.split("_")[0] : orderId;

            System.out.println("[Payment] Success: OrderId=" + orderId + ", Bill=" + billNumber);

            try {
                Optional<Bill> billOpt = billingService.findByNumber(billNumber);
                if (billOpt.isPresent()) {
                    billingService.recordPayment(billOpt.get().getBillId(), PaymentMethod.ONLINE);
                    resp.sendRedirect("/payment_success.html?billId=" + billOpt.get().getBillId());
                } else {
                    System.err.println("[Payment] Error: Bill not found for number " + billNumber);
                    resp.sendRedirect("/payment_error.html?error=BillNotFound");
                }
            } catch (Exception e) {
                e.printStackTrace();
                resp.sendRedirect("/payment_error.html?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
            }
        } else {
            String respMsg = req.getParameter("RESPMSG");
            String status = req.getParameter("STATUS");
            System.err.println("[Payment] Failure: Status=" + status + ", Msg=" + respMsg);
            resp.sendRedirect(
                    "/payment_error.html?status=" + status + "&msg="
                            + java.net.URLEncoder.encode(respMsg != null ? respMsg : "Transaction Failed", "UTF-8"));
        }
    }
}

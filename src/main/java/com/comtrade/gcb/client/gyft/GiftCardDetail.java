package com.comtrade.gcb.client.gyft;

/**
 * Created by muros on 31.8.2016.
 */
public class GiftCardDetail {

    private String cardId;
    private String currencyCode;
    private int price;
    private String merchantName;
    private String mercnantDescription;
    private String merchantIconUrl;
    private String merchantCardImageUrl;

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public int getPrice() {
        return price;
    }

    public String getPriceAsString() {
        return String.valueOf(price / 100);
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setPriceAsString(String price) {
        this.price = Integer.parseInt(price) * 100;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMercnantDescription() {
        return mercnantDescription;
    }

    public void setMercnantDescription(String mercnantDescription) {
        this.mercnantDescription = mercnantDescription;
    }

    public String getMerchantIconUrl() {
        return merchantIconUrl;
    }

    public void setMerchantIconUrl(String merchantIconUrl) {
        this.merchantIconUrl = merchantIconUrl;
    }

    public String getMerchantCardImageUrl() {
        return merchantCardImageUrl;
    }

    public void setMerchantCardImageUrl(String merchantCardImageUrl) {
        this.merchantCardImageUrl = merchantCardImageUrl;
    }
}

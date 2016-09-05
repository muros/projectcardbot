package com.comtrade.gcb.server.controller;

import com.comtrade.gcb.*;
import com.comtrade.gcb.client.gyft.*;
import com.comtrade.gcb.client.gyft.Category;
import com.comtrade.gcb.data.jpa.Transaction;
import com.comtrade.gcb.data.jpa.TransactionType;
import com.comtrade.gcb.data.jpa.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Controller
@EnableAutoConfiguration
@ComponentScan("com.comtrade.*")
@EntityScan("com.comtrade.*")
public class GcbController {

    @Autowired
    UserRepository userRepo;

    @Autowired
    TransactionRepository transactionRepo;

    @Autowired
    GyftClient gyftClient;

    @Autowired
    AuthorizationClient authClient;

    @Autowired
    StripePaymentClient stripe;

    @RequestMapping(path="/user", method=RequestMethod.GET)
    public @ResponseBody List<User> getUsers() {
        List<User> users = new ArrayList<>();

        Iterable<User> allUsersIter = userRepo.findAll();
        allUsersIter.forEach(users::add);

        return users;
    }

    @RequestMapping(path="/user/{id}", method=RequestMethod.GET)
    public @ResponseBody User getUser(@PathVariable("id") String id) {
        User user = null;

        if (id != null) {
            user = userRepo.findOne(id);
        }

        return user;
    }

    @RequestMapping(path="/health", method=RequestMethod.GET)
    public @ResponseBody Boolean healthCheck() {
        boolean health = gyftClient.healthCheck();

        return health;
    }

    @RequestMapping(path="/cards", method=RequestMethod.GET)
    public @ResponseBody List<ShopCard> shopCards() {
        List<ShopCard> cards = new ArrayList<>();
        cards = gyftClient.shopCards();

        return cards;
    }

    @RequestMapping(path="/categories", method=RequestMethod.GET)
    public @ResponseBody List<GiftCategory> categories() {
        List<GiftCategory> categories = new ArrayList<>();
        List<Detail> details = gyftClient.categories();

        details.forEach(cat -> {
            GiftCategory category = new GiftCategory();
            category.setProvider("gyft");
            category.setId("gyft-"+cat.getId());
            category.setName(cat.getName());
            categories.add(category);
        });

        return categories;
    }

    @RequestMapping(path="/merchants", method=RequestMethod.GET)
    public @ResponseBody List<GiftMerchant> merchants() {
        List<GiftMerchant> merchants = new ArrayList<>();
        List<Detail_> gyftMerchants = gyftClient.merchants();

        gyftMerchants.forEach(mer -> {
            GiftMerchant merchant = new GiftMerchant();
            merchant.setProvider("gyft");
            merchant.setId(mer.getId());
            merchant.setName(mer.getName());
            merchant.setCountryCode(mer.getCountryCode());
            merchant.setDescription(mer.getDescription());
            merchant.setCardName(mer.getCardName());
            merchant.setIconUrl(mer.getIconUrl());
            merchant.setCardImageUrl(mer.getCoverImageUrlHd());
            // Cards
            List<ShopCard> gyftCards = mer.getShopCards();
            List<com.comtrade.gcb.Card> cards = new ArrayList<>();
            gyftCards.forEach(car -> {
                com.comtrade.gcb.Card card = new com.comtrade.gcb.Card();
                card.setId("gyft-"+car.getId());
                card.setCurrencyCode(car.getCurrencyCode());
                card.setPrice(car.getPrice());
                cards.add(card);
            });
            merchant.setCards(cards);
            // Categories
            List<Category> gyftCategories = mer.getCategories();
            List<com.comtrade.gcb.Category> categories = new ArrayList<>();
            gyftCategories.forEach(cat -> {
                com.comtrade.gcb.Category category = new com.comtrade.gcb.Category();
                category.setId("gyft-"+cat.getId());
                category.setName(cat.getName());
                categories.add(category);
            });
            merchant.setCategories(categories);
            merchants.add(merchant);
        });

        return merchants;
    }

    @RequestMapping(path="/purchase", method=RequestMethod.POST)
    public @ResponseBody GiftCard purchaseCard(@RequestParam("recipientId") String recipientId,
                                               @RequestParam("messageId") String messageId,
                                               @RequestParam("locale") String locale,
                                               @RequestParam("cardId") String cardId,
                                               @RequestParam("paymentToken") String paymentToken,
                                               @RequestParam("recipientEmail") String recipientEmail,
                                               @RequestParam("notes") String notes,
                                               @RequestParam("firstName") String firstName,
                                               @RequestParam("lastName") String lastName) {
        if (authClient.isUserAuthorized(locale)) {
            String cardIdOnly = "";
            String cardIdParts[] = cardId.split("-");
            if ((cardIdParts != null) && (cardIdParts.length == 2)) {
                cardIdOnly = cardIdParts[1];
            }
            GiftCard giftCard = gyftClient.purchaseCard(cardIdOnly, recipientEmail, "no-ref",
                    notes, firstName, lastName, null, null);
            List<ShopCard> cards = new ArrayList<>();
            cards = gyftClient.shopCards();

            ShopCard theCard = null;
            for (ShopCard card: cards) {
                if (card.getId().equals(cardIdOnly)) {
                    theCard = card;
                }
            }
            String chargeToken = "";
            if (giftCard != null) {
                String currency = "usd";
                Double price = theCard.getOpeningBalance();
                chargeToken = stripe.checkout(paymentToken, price.intValue() * 100, currency, "$" + price.intValue(),
                        messageId, recipientId);
            }

            Transaction transaction = new Transaction();
            transaction.setRecipientId(recipientId);
            transaction.setMessageId(messageId);
            if (theCard != null) {
                transaction.setAmount( (int)(theCard.getOpeningBalance() * 100) );
            }
            transaction.setCardId(String.valueOf(cardId));
            transaction.setReferenceId(giftCard.getCardNumber());
            transaction.setType(TransactionType.PURCHASE);
            transaction.setCardKey(giftCard.getCardKey());
            transaction.setPaymentReference(chargeToken);
            transaction.setLocale(locale);
            transaction.setTimestamp(Calendar.getInstance().getTime());
            transactionRepo.save(transaction);

            return giftCard;
        } else {
            throw new RestClientException("User not authorized for gift card purchase.");
        }
    }

    @RequestMapping(path="/account", method=RequestMethod.GET)
    public @ResponseBody
    Account account() {
        Account account = null;
        account = gyftClient.getAccount();

        return account;
    }

    @RequestMapping(path="/card/{cardKey}", method=RequestMethod.GET)
    public @ResponseBody GiftCard getCard(@PathVariable("cardKey") String cardKey) {
        GiftCard card = gyftClient.getPurchasedCardDetails(cardKey);

        return card;
    }

    @ExceptionHandler({RestClientException.class, NullPointerException.class})
    void handleBadRequests(HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.FORBIDDEN.value());
    }

}

import requests
from meya import Component
from meya.cards import Card, Cards, Button

class GenericCards(Component):

    def start(self):
        
        type_description = self.db.flow.get('team') or ""
        type_description = type_description.lower().strip()
        cardsToDisplay = [[]]
        if 'featured' == type_description :
            cardsToDisplay = [["gyft-4698","gyft-4614"],["gyft-3558","gyft-3559","gyft-4061"],["gyft-4068","gyft-4069","gyft-2974"]]
        if 'restaurants' == type_description :
            cardsToDisplay = [["gyft-4698","gyft-4614"],["gyft-3558","gyft-3559","gyft-4061"],["gyft-4068","gyft-4069","gyft-2974"]]
        if 'retail stores' == type_description :
            cardsToDisplay = [["gyft-4698","gyft-4614"],["gyft-3558","gyft-3559","gyft-4061"],["gyft-4068","gyft-4069","gyft-2974"]]
        if 'entertainment' == type_description :
            cardsToDisplay = [["gyft-4698","gyft-4614"],["gyft-3558","gyft-3559","gyft-4061"],["gyft-4068","gyft-4069","gyft-2974"]]
        if 'home goods' == type_description :
            cardsToDisplay = [["gyft-4698","gyft-4614"],["gyft-3558","gyft-3559","gyft-4061"],["gyft-4068","gyft-4069","gyft-2974"]]
        if 'students' == type_description :
            cardsToDisplay = [["gyft-4698","gyft-4614"],["gyft-3558","gyft-3559","gyft-4061"],["gyft-4068","gyft-4069","gyft-2974"]]
        if len(cardsToDisplay) == 0 :
            return self.respond(message=None, action="no_card")

        # construct a list of cards (they are `generic` template in Messenger)
        elements = []
        nCards = len(cardsToDisplay)
        for cardIdx in range(nCards):
            nValues = len(cardsToDisplay[cardIdx])
            cardName = ""
            cardImage = ""
            buttons = []
            for valueIdx in range(nValues) :
                response = requests.get("http://ec2.urkei.com:9091/v1/giftbot/carddetail?cardId="+cardsToDisplay[cardIdx][valueIdx])
                cardName = response.json()['merchantName']
                cardImage = response.json()['merchantCardImageUrl']
                cardValue = response.json()['priceAsString']
                cardCurr = response.json()['currencyCode']
                # Buttons for values
                buttons.append(Button(text=cardCurr+" "+cardValue, action="next", data={'cardId': cardsToDisplay[cardIdx][valueIdx]}))
                
            element = Card(
                title = cardName,
                text = "Gyft card",
                image_url = cardImage,
                buttons = buttons
            )
            elements.append(element)

        # `Cards` is a multi card element in Messenger
        card = Cards(elements=elements)

        # create the message (note the `card` rather than `text`)
        message = self.create_message(card=card)

        # respond as you normally would
        return self.respond(message=message)
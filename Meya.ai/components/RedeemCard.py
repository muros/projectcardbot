import requests
from meya import Component
from meya.cards import Card, Cards, Button

class RedeemCard(Component):

    def start(self):
        
        uid = self.db.flow.get('uuid')
        # Call middleware to retrieve card data
        try:
            response = requests.get("http://ec2.urkei.com:9091/v1/giftbot/redeem?secret=cosmicsecret&uuid="+uid)
            print response
            cardName = response.json()['name']
            cardKey = response.json()['card_key']
            pin = response.json()['pin']
            cardNumber = response.json()['card_number']
            cardImage = response.json()['image']
            print cardName
        except:
            text = "Payment issue, try to redeem later."
            message = self.create_message(text=text)
            return self.respond(message=message, action="no_card")
        
        buttons = [
            Button(type="share")
        ]
        # construct a list of cards (they are `generic` template in Messenger)
        elements = []
        element = Card(
            title=cardName,
            text=cardNumber+" / "+pin,
            item_url=cardKey,
            image_url=cardImage,
            buttons=buttons
        )
        elements.append(element)

        # `Cards` is a multi card element in Messenger
        card = Cards(elements=elements)

        # create the message (note the `card` rather than `text`)
        message = self.create_message(card=card)

        # respond as you normally would
        return self.respond(message=message, action="yes_card")
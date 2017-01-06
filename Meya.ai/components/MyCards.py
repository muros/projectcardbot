import requests
from meya import Component
from meya.cards import Card, Cards, Button

class RedeemCard(Component):

    def start(self):
        
        uid = self.db.user.id
        numItems = 0
        # Call middleware to retrieve card data
        try:
            response = requests.get("https://ec2.urkei.com/v1/giftbot/usercards?secret=cosmicsecret&userId="+uid)
            numItems = len(response.json())
        except:
            text = "Issue wihth card retreival."
            message = self.create_message(text=text)
            return self.respond(message=message, action="no_card")
        
        # construct a list of cards (they are `generic` template in Messenger)
        elements = []
        for x in range(numItems):
            cardName = response.json()[x]['name']
            cardKey = response.json()[x]['card_key']
            pin = response.json()[x]['pin']
            cardNumber = response.json()[x]['card_number']
            cardImage = response.json()[x]['image']
            print cardName
            # Single button
            buttons = [
                Button(text="Go to card", url=cardKey)
            ]
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
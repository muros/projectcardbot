import requests
from meya import Component
from meya.cards import Card, Cards, Button

class StripePay(Component):

    def start(self):
        
        uid = self.db.flow.get('uuid')
        userId = self.db.user.id
        cardId = self.db.flow.get('cardId')
        
        print uid
        print userId
        print cardId
        # Single button
        buttons = [
            Button(text="Pay", url="http://ec2.urkei.com:9091/v1/giftbot/stripe/card?cardId="+cardId+"&uuid="+uid+"&userId="+userId)
        ]
        # construct a list of cards (they are `generic` template in Messenger)
        elements = []
        element = Card(
            title="Stripe", text="Gateway", image_url="http://ec2.urkei.com:9091/v1/giftbot/stripe.png",
            buttons=buttons
        )
        elements.append(element)

        # `Cards` is a multi card element in Messenger
        card = Cards(elements=elements)

        # create the message (note the `card` rather than `text`)
        message = self.create_message(card=card)

        # respond as you normally would
        return self.respond(message=message, action="next")
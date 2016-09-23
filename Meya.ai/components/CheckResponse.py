# -*- coding: utf-8 -*-
import requests
from meya import Component


class CheckResponse(Component):

    def start(self):
        action = "notnx"
        answ = self.db.flow.get('value')
        print answ
        if answ == "Yes please" :
            action = "yesplease"
        if answ == "No thank you":
            action = "notnx"
            
        return self.respond(message=None, action=action)
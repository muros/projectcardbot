# -*- coding: utf-8 -*-
import uuid
from meya import Component

class GenerateUUID(Component):

    def start(self):
      # code goes here
      uid = uuid.uuid1()
      uid_str = uid.urn
      uidstr = uid_str[9:]
      print self.db.flow.set('uuid', uidstr)
      return self.respond(message=None, action="next")
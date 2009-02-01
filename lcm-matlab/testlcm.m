lc = lcm.LCM()
disp 'created'
subs = lc.subscribe('FOO', 'test-user-data')
disp 'subscribed'
lc.publish('FOO', 'asdf')
disp 'published'
msgs = lc.get_messages()
disp 'handled'
lc.unsubscribe(subs)
disp 'unsubscribed'
lc.close()
disp 'closed'

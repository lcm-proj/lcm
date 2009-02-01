lcm = lcm_create()
disp 'created'
subs = lcm_subscribe(lcm, 'FOO', 'test-user-data')
disp 'subscribed'
lcm_publish(lcm, 'FOO', 'asdf')
disp 'published'
msgs = lcm_get_messages(lcm)
disp 'handled'
lcm_unsubscribe(lcm, subs)
disp 'unsubscribed'
lcm_destroy(lcm)
disp 'destroyed'

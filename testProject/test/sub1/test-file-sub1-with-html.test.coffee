letTestPass = false #switch for development

test 'test description', ->
  ok(letTestPass, 'assertion message')


module "module 1"

test 'test description test 1 module 1', ->
  equal(4, (if letTestPass then 4 else 3), 'assertion message 1 test 1 module 1')
  ok(letTestPass, 'assertion message 2 test 1 module 1')

test 'test description test 2 module 1', ->
  ok(letTestPass, 'assertion message test 2 module 1')


module "module 2"

test 'test description test 1 module 2', ->
  ok(letTestPass, 'assertion message test 1 module 2')

test 'test description test 2 module 2', ->
  equal(4, 4, 'assertion message test 2 module 2')

curl --location --request POST \
'http://192.168.0.106:14000/injectInputEvent?action=0&pointerId=-2&x=159&y=204&width=1200&height=2664&displayId=0&actionButton=1&buttons=1'
# sleep 0.1
# curl --location --request POST \
# 'http://192.168.0.106:14000/injectInputEvent?action=2&pointerId=-2&x=300&y=300&width=1200&height=2664&displayId=0&actionButton=0&buttons=1'
sleep 0.1
curl --location --request POST \
'http://192.168.0.106:14000/injectInputEvent?action=1&pointerId=-2&x=159&y=204&width=1200&height=2664&displayId=0&actionButton=1&buttons=0'
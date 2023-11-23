ip="192.168.17.35"
curl --location --request POST "http://$ip:14000/createVirtualDisplay?width=1920&height=1080&density=100"
# curl --location --request POST "http://$ip:14000/resize_vd?width=1920&height=1080&density=100&id=12"
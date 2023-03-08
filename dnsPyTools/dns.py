from scapy.all import *
from scapy.layers.dns import DNS, DNSQR, DNSRR

dns_packets = rdpcap('uploadPic.pcap')
for packet in dns_packets:
    if packet.haslayer(DNS):
        if packet[DNS].qr == 1:
            print(packet[DNS].name)
            print(packet[DNSQR].qname)
            print(packet[DNS].ancount)
            for x in range(packet[DNS].ancount):
                print(packet[DNSRR][x].rdata)

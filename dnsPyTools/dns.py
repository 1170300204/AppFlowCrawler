import sys
from scapy.all import *

def extractSNIFromPcap(pcapfile,snifile):
  load_layer("tls")
  try:
    reader = PcapReader(pcapfile)
    fp = open(snifile,'w+')
    for pkt in reader:
      if pkt.haslayer(TCP) and pkt.haslayer(TLS) and pkt.haslayer('TLS Handshake - Client Hello') and pkt.haslayer('TLS Extension - Server Name'):
        pkt_servername = pkt['TLS']['TLS Handshake - Client Hello']['TLS Extension - Server Name']
        out = pkt['IP'].dst + '\t' + pkt_servername.fields['servernames'][0].fields['servername'].decode('utf8')
        print(out,file=fp)
    fp.flush()
    fp.close()
  except Exception as error:
    print("packet processing error: %s", error)
    fp.flush()
    fp.close()
    sys.exit(-1)

if __name__ == '__main__':
  extractSNIFromPcap(sys.argv[1], sys.argv[2])

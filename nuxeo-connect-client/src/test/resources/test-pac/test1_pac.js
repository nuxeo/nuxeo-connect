function FindProxyForURL(url, host) {
  if (isInNet(dnsResolve(host), "79.0.0.0", "82.0.0.0")) {
    return "DIRECT";
  }
  else {
    return "PROXY 127.0.0.1";
  }
}

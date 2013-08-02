function FindProxyForURL(url, host) {

  if (dnsDomainIs(host, "intranet.domain.com") ||
      shExpMatch(host, "(*.abcdomain.com|abcdomain.com)")) {
    return "DIRECT";
  }

  if (url.substring(0, 4) == "ftp:" ||
      shExpMatch(url, "http://abcdomain.com/folder/*")) {
    return "DIRECT";
  }

  return "PROXY 4.5.6.7:8080; PROXY 7.8.9.10:8080";
}

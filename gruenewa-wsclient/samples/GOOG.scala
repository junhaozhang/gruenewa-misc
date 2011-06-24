import java.net.URL
import gruenewa.wsclient.{Service, SoapDispatch}

val url =
  new URL("http://www.restfulwebservices.net/wcf/StockQuoteService.svc")

val service = new Service(url) with SoapDispatch

val symbol =  "GOOG"

val request = {
  <soapenv:Envelope
  xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
  xmlns:ns="http://www.restfulwebservices.net/ServiceContracts/2008/01">
    <soapenv:Header/>
    <soapenv:Body>
      <ns:GetStockQuote>
        <ns:request>{ symbol }</ns:request>
      </ns:GetStockQuote>
    </soapenv:Body>
  </soapenv:Envelope>
}

val response = service.dispatch(
  message = request,
  soapAction = Some("GetStockQuote"))

println(" >>> " + (response \\ "Name").text + ": " + (response \\ "Last").text)

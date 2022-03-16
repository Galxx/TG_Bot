package org.group_gb.tg_bot.ipgeolocation_api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.group_gb.tg_bot.exceptions.IpgeolocationAPIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalTime;

@Service
@Slf4j
public class IpgeolocationAPIService  {

    @Value("${ipgeolocation.token}")
    String token;

    public int getHour(Double lat, Double lon) throws IpgeolocationAPIException{
        WebClient webClient =  WebClient
                .builder()
                .baseUrl("https://api.ipgeolocation.io")
                .build();

        try {

            log.info( "Request pgeolocationAPI param lat " + lat.toString() + "lon "+lon.toString());

            String responseJson = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/timezone")
                            .queryParam("lat", lat.toString())
                            .queryParam("long", lon.toString())
                            .queryParam("apiKey", token)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info(responseJson);

            JsonObject jsonObject = JsonParser.parseString(responseJson).getAsJsonObject();

            return LocalTime.parse(jsonObject.get("time_24").getAsString()).getHour();
        }catch (RuntimeException e){
            throw new IpgeolocationAPIException(e.getMessage());
        }

    }

}

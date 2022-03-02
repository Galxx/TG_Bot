package org.group_gb.tg_bot.ipgeolocationAPI;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalTime;

@Service
public class IpgeolocationAPIService {

    @Value("${ipgeolocation.token}")
    String token;

    public int getHour(Double lat, Double lon){
        WebClient webClient =  WebClient
                .builder()
                .baseUrl("https://api.ipgeolocation.io")
                .build();

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
        //TODO 2 Сделать проверку на статус код сообщения

        JsonObject jsonObject = JsonParser.parseString(responseJson).getAsJsonObject();

        return LocalTime.parse(jsonObject.get("time_24").getAsString()).getHour();

    }


}

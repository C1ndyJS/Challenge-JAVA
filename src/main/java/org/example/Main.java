package org.example;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import com.google.gson.Gson;

record ExchangeRateResponse(String result, String base_code, Map<String, Double> conversion_rates) {}

class ExchangeRateClient {
    private static final String API_KEY = "TU API KEY "; // Cambia por tu API Key
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/";
    HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public ExchangeRateResponse getRates(String baseCurrency) throws IOException, InterruptedException {
        String url = BASE_URL + baseCurrency;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(response.body(), ExchangeRateResponse.class);
    }
}

// Clase para la lógica de conversión
class CurrencyConverter {
    private final ExchangeRateClient client;

    public CurrencyConverter(ExchangeRateClient client) {
        this.client = client;
    }

    public double convert(String from, String to, double amount) {
        try {
            ExchangeRateResponse rates = client.getRates(from);
            Double rate = rates.conversion_rates().get(to);
            if (rate == null) throw new RuntimeException("Moneda destino no encontrada: " + to);
            return amount * rate;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return -1;
        }
    }
}
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ExchangeRateClient client = new ExchangeRateClient();
        CurrencyConverter converter = new CurrencyConverter(client);

        while (true) {
            System.out.println("\n=== CONVERSOR DE MONEDAS ===");
            System.out.println("1. Convertir moneda");
            System.out.println("2. Salir");
            System.out.print("Elija una opción: ");
            int opcion = scanner.nextInt();
            scanner.nextLine(); // limpiar buffer

            if (opcion == 2) {
                System.out.println("¡Hasta luego!");
                break;
            }

            if (opcion == 1) {
                System.out.print("Ingrese moneda origen (ej: USD): ");
                String from = scanner.nextLine().toUpperCase();

                System.out.print("Ingrese moneda destino (ej: EUR): ");
                String to = scanner.nextLine().toUpperCase();

                System.out.print("Ingrese cantidad: ");
                double amount = scanner.nextDouble();

                double resultado = converter.convert(from, to, amount);

                if (resultado != -1) {
                    System.out.printf("%.2f %s = %.2f %s%n", amount, from, resultado, to);
                }
            } else {
                System.out.println("Opción inválida.");
            }
        }
    }
}
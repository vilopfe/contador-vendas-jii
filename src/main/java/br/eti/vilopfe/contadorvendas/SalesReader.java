package br.eti.vilopfe.contadorvendas;

import com.opencsv.bean.CsvToBeanBuilder;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

public class SalesReader {

    private final List<Sale> sales;

    public SalesReader(String salesFile) {

        final var dataStream = ClassLoader.getSystemResourceAsStream(salesFile);

        if (dataStream == null) {
            throw new IllegalStateException("File not found or is empty");
        }

        final var builder = new CsvToBeanBuilder<Sale>(new InputStreamReader(dataStream, StandardCharsets.ISO_8859_1));

        sales = builder
                .withType(Sale.class)
                .withSeparator(';')
                .build()
                .parse();
    }

    public void totalCompletedSales() {
        // TODO mostrar o total (em R$) de vendas completas
        final var completedSales = sales.stream()
                .filter(sales -> sales.isCompleted()) //Limitando ocorrencias da lista
                .map(Sale::getValue) //Mapeando atributo que deseja selecionar
                .reduce(BigDecimal.ZERO, BigDecimal::add); //
        System.out.println("Valor total em vendas completas: R$ " + completedSales);
    }

    public void totalCancelledSales() {
        // TODO mostrar o total (em R$) de vendas canceladas
        final var cancelledSales = sales.stream()
                .filter(sales -> sales.isCancelled())
                .map(Sale::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println("Valor total em vendas canceladas: R$ " + cancelledSales);
    }

    public void mostRecentSale() {
        // TODO encontrar qual foi a data da primeira venda
        final var minDate = sales.stream()
                .map(Sale::getSaleDate)
                .min(comparing(LocalDate::toEpochDay))
                .get();
        System.out.println("Data da primeira venda: " + minDate);
    }

    public void daysBetweenFirstAndLast() {
        // TODO calcular qual a quantidade de dias entre a primeira e a ultima venda
        final var minDate = sales.stream()
                .map(Sale::getSaleDate)
                .min(comparing(LocalDate::toEpochDay))
                .get();
        final var maxDate = sales.stream()
                .map(Sale::getSaleDate)
                .max(comparing(LocalDate::toEpochDay))
                .get();
        final var daysBetweenFirstAndLast = minDate.datesUntil(maxDate)
                .count();

        System.out.println("Quantidade de dias entre a primeira e a ultima venda é de " + daysBetweenFirstAndLast);
    }

    public void totalSalesBySeller(String sellerName) {
        // TODO encontrar o total (em R$) de vendas do vendedor recebido por parametro
        final var totalSalesBySeller = sales.stream()
                .filter(sales -> sales.getSeller().equals(sellerName))
                .map(Sale::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println("Total de vendas do vendedor " + sellerName + " é de R$" + totalSalesBySeller);
    }

    public void countSalesByManager(String managerName) {
        // TODO contar a quantidade de vendas para o gerente recebido por parametro
        final var countSalesByManager = sales.stream()
                .filter(sales -> sales.getManager().equals(managerName))
                .count();
        System.out.println("Total de vendas do gerente " + managerName + " é de " + countSalesByManager + " vendas");
    }

    public void totalSalesByMonth(Month... months) {
        // TODO totalizar o valor (em R$) de vendas para os meses informados por parametro
         final var totalSalesByMonth = sales.stream()
                    .filter(sales -> sales.getSaleDate().getMonth().equals(months[0]) || sales.getSaleDate().getMonth().equals(months[1]))
                    .map(Sale::getValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
         System.out.println("Total de vendas dos meses " + months[0] + " e " + months[1] + " é de R$" + totalSalesByMonth);
    }

    public void rankingByDepartment() {
        // TODO faca um ranking contando o total (quantidade) de vendas por departamento
        // Mapeando: <Departamento, Count>
        Map<String,Long> rankingByDepartment = sales.stream()
                .collect(Collectors.groupingBy(Sale::getDepartment,Collectors.counting()));
        final var rankingByDepartmentSort = rankingByDepartment
                .entrySet()
                .stream()
                //Classificação descendente
                .sorted(Map.Entry.<String,Long>comparingByValue().reversed())
                    //Mapeando por Departamento
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (c1, c2) -> c1,
                        LinkedHashMap::new));
        System.out.println("Ranking total de vendas por detpartamento : " + rankingByDepartmentSort);
    }

    public void rankingByPaymentMethod() {
        // TODO faca um ranking contando o total (quantidade) de vendas por meio de pagamento
        // Mapeando: <Departamento, Count>
        Map<String,Long> rankingByPaymentMethod = sales.stream()
                .collect(Collectors.groupingBy(Sale::getPaymentMethod,Collectors.counting()));
        final var rankingByPaymentMethodSort = rankingByPaymentMethod
                .entrySet()
                .stream()
                //Classificação descendente
                .sorted(Map.Entry.<String,Long>comparingByValue().reversed())
                //Mapeando por Departamento
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (c1, c2) -> c1,
                        LinkedHashMap::new));
        System.out.println("Ranking total de vendas por tipo de pagamento : " + rankingByPaymentMethodSort);
    }

    public void bestSellers() {
        // TODO faca um top 3 dos vendedores que mais venderam (ranking por valor em vendas)
        // Mapeando: <Departamento, Count>

        Map<String,Double> rankingBySeller = sales.stream()
                .collect(Collectors.groupingBy(Sale::getSeller, Collectors.summingDouble((Sale::getValueDouble))));
        final var rankingBySellerSort = rankingBySeller
                .entrySet()
                .stream()
                //Classificação descendente
                .sorted(Map.Entry.<String,Double>comparingByValue().reversed())
                .limit(3)
                //Mapeando por Departamento
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (c1, c2) -> c1,
                        LinkedHashMap::new));
        System.out.println("Ranking total de vendas por vendedor : " + rankingBySellerSort);
    }

    /*
     * Use esse metodo para converter objetos BigDecimal para uma represetancao de moeda
     */
    private String toCurrency(BigDecimal value) {
        return NumberFormat.getInstance().format(value);
    }
}

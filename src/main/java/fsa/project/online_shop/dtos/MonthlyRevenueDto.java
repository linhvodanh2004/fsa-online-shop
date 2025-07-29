package fsa.project.online_shop.dtos;

public class MonthlyRevenueDto {
    private Integer month;
    private Double total;

    public MonthlyRevenueDto(Integer month, Double total) {
        this.month = month;
        this.total = total;
    }

    public Integer getMonth() {
        return month;
    }

    public Double getTotal() {
        return total;
    }
}


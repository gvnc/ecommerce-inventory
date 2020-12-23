package ecommerce.app.backend.repository.model;

public interface SalesHistory {

    String getInsertDate();

    String getMarketPlace();

    Integer getQuantity();

    default String getLogDate(){
        String insertDate = getInsertDate();
        if(insertDate != null && insertDate.length() >= 10)
            return insertDate.substring(0,10);
        return insertDate;
    }
}

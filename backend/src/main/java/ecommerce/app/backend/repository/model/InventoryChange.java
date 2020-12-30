package ecommerce.app.backend.repository.model;

public interface InventoryChange {

    String getInsertDate();

    String getMarketPlace();

    Integer getQuantity();

    String getOrderType();

    default String getLogDate(){
        String insertDate = getInsertDate();
        if(insertDate != null && insertDate.length() >= 10)
            return insertDate.substring(0,10);
        return insertDate;
    }
}

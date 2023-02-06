package com.webshopJ.orderservice.service;

import com.netflix.appinfo.InstanceInfo;
import com.webshopJ.orderservice.dto.InventoryResponse;
import com.webshopJ.orderservice.dto.OrderLineItemsDto;
import com.webshopJ.orderservice.dto.OrderRequest;
import com.webshopJ.orderservice.model.Order;
import com.webshopJ.orderservice.model.OrderLineItem;
import com.webshopJ.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@LoadBalancerClients
public class OrderService {


    private final OrderRepository orderRepository;

    @Autowired
    private final WebClient.Builder webClient;

    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItem> orderLineItemList = orderRequest.getOrderLineItemsDtoList()
                                                            .stream()
                                                            .map(this::mapToDto)
                                                            .collect(Collectors.toList());
         order.setOrderLineItemList(orderLineItemList);


        List<String> skuCodes = order.getOrderLineItemList().stream()
                                     .map(OrderLineItem::getSkuCode)
                                     .toList();

        InventoryResponse[] inventoryResponseArray = webClient.build().get()
                                  .uri("http://inventory-service/api/inventory",uriBuilder -> uriBuilder.queryParam("skuCode",skuCodes).build())
                                  .retrieve()
                                  .bodyToMono(InventoryResponse[].class)
                                  .block();

        Boolean isInStock = Arrays.stream(inventoryResponseArray)
                                  .allMatch(InventoryResponse::getIsInStock);

        if(isInStock){
            orderRepository.save(order);
        }else{
            throw new IllegalArgumentException("Out of stock");
        }
    }

    private OrderLineItem mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItem orderLineItem=new OrderLineItem();
        orderLineItem.setPrice(orderLineItemsDto.getPrice());
        orderLineItem.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItem.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItem;
    }
}

package com.wishlist.cst438project2.service.impl;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.wishlist.cst438project2.common.Constants;
import com.wishlist.cst438project2.document.Item;
import com.wishlist.cst438project2.dto.ItemDTO;
import com.wishlist.cst438project2.exception.BadRequestException;
import com.wishlist.cst438project2.exception.ExternalServerException;
import com.wishlist.cst438project2.integration.FirebaseIntegration;
import com.wishlist.cst438project2.service.ItemService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FirebaseIntegration firebaseIntegration;

    /**
     * database record creation route for item. Each new item will be set to an auto-incremented id
     * <p>
     * returns timestamp of record creation.
     */
    @SneakyThrows
    @Override
    public String createItem(ItemDTO itemDTO) {
        log.info("ItemServiceImpl: starting createItem");
        
        ItemDTO dbItemDTO = firebaseIntegration.getItem(itemDTO.getName());
        if (Objects.nonNull(dbItemDTO)) {
            throw new BadRequestException(Constants.ERROR_ITEM_ALREADY_EXISTS.replace(Constants.KEY_ITEM_NAME, itemDTO.getName()));
        }
        
        Item item = modelMapper.map(ItemDTO, Item.class);
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection(Constants.DOCUMENT_ITEM).document(item.getName()).set(item);
        String responseTimeStamp = collectionsApiFuture.get().getUpdateTime().toString();
        
        log.info("ItemServiceImpl: createItem: responseTimeStamp: {}", responseTimeStamp);
        log.info("ItemServiceImpl: exiting createItem");
        return responseTimeStamp;
    }
}
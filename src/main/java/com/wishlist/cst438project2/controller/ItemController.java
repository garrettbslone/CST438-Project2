package com.wishlist.cst438project2.controller;

import com.wishlist.cst438project2.common.Constants;
import com.wishlist.cst438project2.document.Item;
import com.wishlist.cst438project2.dto.ItemDTO;
import com.wishlist.cst438project2.exception.BadRequestException;
import com.wishlist.cst438project2.integration.FirebaseIntegration;
import com.wishlist.cst438project2.service.ItemService;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * API endpoints for handling item entities in database
 * <p>
 * referenced:
 *     - https://howtodoinjava.com/spring5/webmvc/controller-getmapping-postmapping/
 *     - https://www.youtube.com/watch?v=8jK9O0lwem0
 *     - https://stackoverflow.com/questions/42546141/add-location-header-to-spring-mvcs-post-response
 *     - Chaitanya's code and advice
 * @author Barbara Kondo
 * @version %I% %G%
 */
@RestController
//@RequestMapping("/items")
@Slf4j
public class ItemController {
    @Autowired
    private ItemService itemService;

    @Autowired
    private FirebaseIntegration firebaseIntegration;

    /**
     * POST request to create an item in the database.
     * returns item creation timestamp
     */
    @PostMapping("/items")
    public String createItem(@RequestBody ItemDTO itemDTO) {
        log.info("ItemController: Starting createItem");
        try {
            if (Objects.isNull(itemDTO) || (itemDTO.getName().isBlank() || Objects.isNull(itemDTO.getUserId()))) {
                throw new BadRequestException();
            } else {
                log.info("\n    name: " + itemDTO.getName() + "\n" + "    link: " + itemDTO.getLink() + "\n"
                        + "    description: " + itemDTO.getDescription() + "\n" + "    imgUrl: "
                        + itemDTO.getImgUrl() + "\n" + "    userId: " + itemDTO.getUserId());
                String timestamp = itemService.createItem(itemDTO);
                log.info("ItemController: exiting successful createItem");
                return timestamp;
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }
}

package com.wishlist.cst438project2.integration;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.wishlist.cst438project2.common.Constants;
import com.wishlist.cst438project2.document.Item;
import com.wishlist.cst438project2.document.User;
import com.wishlist.cst438project2.document.Wishlist;
import com.wishlist.cst438project2.dto.ItemDTO;
import com.wishlist.cst438project2.dto.UserDTO;
import com.wishlist.cst438project2.exception.BadRequestException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class FirebaseIntegration {

    public final Firestore dbFirestore = FirestoreClient.getFirestore();

    @Autowired
    private ModelMapper modelMapper;

    @SneakyThrows
    public UserDTO getUser(String username) {

        log.info("FirebaseIntegration: Starting getUser");

        DocumentReference documentReference = dbFirestore.collection(Constants.DOCUMENT_USER).document(username);

        ApiFuture<DocumentSnapshot> snapshotApiFuture = documentReference.get();

        try {

            DocumentSnapshot documentSnapshot = snapshotApiFuture.get();

            User user = null;
            if (documentSnapshot.exists()) {
                user = documentSnapshot.toObject(User.class);
            }

            log.info("FirebaseIntegration: Exiting getUser");

            return user == null ? null : modelMapper.map(user, UserDTO.class);

        } catch(Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    @SneakyThrows
    public List<UserDTO> getAllUsers() {

        log.info("FirebaseIntegration: Starting getAllUsers");

        List<UserDTO> userDTOList = new ArrayList<>();

        try {

            ApiFuture<QuerySnapshot> future = dbFirestore.collection(Constants.DOCUMENT_USER).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if(!documents.isEmpty()) {

                log.info("User count: " + documents.size());

                for(QueryDocumentSnapshot document: documents) {
                    userDTOList.add(document.toObject(UserDTO.class));
                }
            }

            log.info("FirebaseIntegration: Exiting getAllUsers");

            return userDTOList;

        } catch(Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    @SneakyThrows
    public void deleteUser(String username) {

        log.info("FirebaseIntegration: Starting deleteUser");

        try {

            ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection(Constants.DOCUMENT_USER).document(username).delete();

            String responseTimestamp = collectionApiFuture.get().getUpdateTime().toString();

            log.info(Constants.USER_DELETED + " {}" , responseTimestamp);

            log.info("FirebaseIntegration: Exiting deleteUser");

        } catch(Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * returns the db item that matches given item name and userId or null if not found
     * <p>
     * NOTE: some items may have the same name but be connected to different users
     * @param name item name to be matched against Item db
     */
    @SneakyThrows
    public ItemDTO getItem(String name, int userId) {
        log.info("FirebaseIntegration: Starting getItem");
        CollectionReference collectionReference = dbFirestore.collection(Constants.DOCUMENT_ITEM);
        Query query = collectionReference.whereEqualTo(Constants.FIELD_ITEM_NAME, name).whereEqualTo(Constants.FIELD_USER_ID, userId);
        ApiFuture<QuerySnapshot> snapshotApiFuture = query.get();

        try {
            QuerySnapshot querySnapshot = snapshotApiFuture.get();
            Item item = null;

            // if item exists, return the item?
            if (querySnapshot.size() > 0) {
                log.info("\nFirebaseIntegration: createItem: querySnapshot:");
                log.info(querySnapshot.toString());
                item = querySnapshot.toObjects(Item.class).get(0);
            }

            log.info("FirebaseIntegration: Exiting getItem");
            return item == null ? null : item.fetchItemDTO();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * return the item document id associated with a given item name and user ID
     * @param name item name
     * @param userId
     */
    @SneakyThrows
    public String getItemDocId(String name, int userId) {
        log.info("FirebaseIntegration: Starting getItemdocId");
        CollectionReference collectionReference = dbFirestore.collection(Constants.DOCUMENT_ITEM);
        Query query = collectionReference.whereEqualTo(Constants.FIELD_ITEM_NAME, name).whereEqualTo(Constants.FIELD_USER_ID, userId);
        ApiFuture<QuerySnapshot> snapshotApiFuture = query.get();

        try {
            QuerySnapshot querySnapshot = snapshotApiFuture.get();
//            log.info(String.format("FirebaseIntegration: getItemDocId:\n    querySnapshot.size(): %d", querySnapshot.size()));

            if ((querySnapshot.size() < 1) || (querySnapshot.size() > 1)) {
                throw new BadRequestException(Constants.ERROR_ITEM_NOT_FOUND);
            }

            log.info(String.format("FirebaseIntegration: getItemdocId: %s", querySnapshot.getDocuments().get(0).getId()));
            log.info("FirebaseIntegration: Exiting getItemdocId");
            return querySnapshot.getDocuments().get(0).getId();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * returns list of all documents within item collection
     */
    @SneakyThrows
    public List<ItemDTO> getAllItems() {
        log.info("FirebaseIntegration: Starting getAllItems");
        List<ItemDTO> collection = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> dbNudge = dbFirestore.collection(Constants.DOCUMENT_ITEM).get();
            List<QueryDocumentSnapshot> documents = dbNudge.get().getDocuments();

            for(QueryDocumentSnapshot snap : documents) {
                collection.add(snap.toObject(ItemDTO.class));
            }

            log.info("FirebaseIntegration: Exiting getAllItems");
            return collection;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * remove the item associated with a given document ID
     * returns timestamp of deletion
     */
    @SneakyThrows
    public String removeItem(String docId) {
        log.info("FirebaseIntegration: Starting removeItem");
        try {
            ApiFuture<WriteResult> writeResult = dbFirestore.collection(Constants.DOCUMENT_ITEM).document(docId).delete();
            String responseTimestamp = writeResult.get().getUpdateTime().toString();
            log.info(Constants.ITEM_REMOVED + " {}" , responseTimestamp);

            log.info("FirebaseIntegration: Exiting removeItem");
            return responseTimestamp;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    @SneakyThrows
    public Wishlist getUserWishlist(String userId) {

        log.info("FirebaseIntegration: Starting getUserWishlist for User: {}", userId);

        DocumentReference documentReference = dbFirestore.collection(Constants.DOCUMENT_USER_WISHLIST).document(userId);

        ApiFuture<DocumentSnapshot> snapshotApiFuture = documentReference.get();

        try {

            DocumentSnapshot documentSnapshot = snapshotApiFuture.get();

            Wishlist wishlist = null;
            if (documentSnapshot.exists()) {
                wishlist = documentSnapshot.toObject(Wishlist.class);
            }

            log.info("FirebaseIntegration: Exiting getUserWishlist");
            return wishlist;
            
        } catch(Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }
}

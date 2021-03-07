package com.ifixhubke.kibu_olx.others;

import com.ifixhubke.kibu_olx.data.Favourites;
import com.ifixhubke.kibu_olx.data.Item;

public interface ItemClickListener {
    void addItemToFavorites(Item item, int position);
    void clickCard(Favourites favourites,int position);
}
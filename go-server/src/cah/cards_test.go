package cah

import (
	"testing"
)

func TestGetNewWhiteCard(t *testing.T) {
	if(GetNewWhiteCard() != wcList[0]) {
		t.Error("TestGetNewCard didn't work as expected")
	} 
}

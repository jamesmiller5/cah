package cah

import (
	"testing"
)

func TestGetNewWhiteCard(t *testing.T) {
	if GetNewWhiteCard() != wcList[0] {
		t.Error("TestGetNewWhiteCard didn't work as expected")
	}
}

func TestGetNewBlackCard(t *testing.T) {
	if GetNewBlackCard() != bcList[0] {
		t.Error("TestGetNewBlackCard didn't work as expected")
	}
}

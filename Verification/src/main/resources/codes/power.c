PRE {x=y&x>0}
	erg := 1;
	WHILE x<>0 DO //erg=2^(y-x)
		erg:=erg*2;
		x:=x-1
	OD
POST {erg=2^y}
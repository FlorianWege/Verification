PRE {n>=1}
	x:=1;
	IF a>0 THEN
	    a:=a+1;
	    b:=b+1
	FI;
	x:=10;
	y:=5;
	IF b<7 THEN
	    y:=y*5;
	    z:=z-1
	FI;
	k:=k
POST {f=n!}
PRE {x>=0}
	y:=1;
	z:=0;
	
	WHILE z!=x DO
		z:=z+1;
		y:=y*z
	OD
POST {y=x!}
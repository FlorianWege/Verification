PRE {x0>0∧y0>0}
	x:=x0;
	y:=y0;
	
	WHILE x != y DO
		IF x<y THEN
			z:=x;
			x:=y;
			y:=z
		FI;
		
		x:=x-y
	OD
POST {x=y∧x=gcd(x0,y0)}
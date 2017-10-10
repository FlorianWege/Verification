PRE {n>=1}
	k := 1;
	f := 1;
	
	//variant func: n-k
	WHILE k < n DO //loop invariant: f=k! && 0<=k && k<=n
		k := k + 1;
		f := f * k
	OD
POST {f=n!}
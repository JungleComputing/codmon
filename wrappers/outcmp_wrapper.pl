#!/usr/bin/perl

# Usage : perl outcmp_wrapper <setdirectory> <referency> <cmd>
# Wrapper script for computation targets.
#   Compare the output of <cmd> against <referency>, in a "parallel-aware" scheme
#   Prints the old/new output if there is a difference
#   and propagates the exit value if there is an error


use strict;

# test if a string is in an array
sub is_in {
    my ($element, $array) = @_;
    my @list = @$array;

    for (my $j = 0; $j <= $#list; $j++) {
#	    print "Action $para[$j] in $out\n";
	if ($element eq $list[$j]) {
#		print "Match !\n";
	    return 1;
	}
    }
    return 0;

}


# protect a string, to be passed a regexp (must backslash interpreted chars)
sub protect {
    my $result = $_[0];
    $result =~ s/\\/\\\\/g; # \
    $result =~ s/\[/\\\[/g; # [
    $result =~ s/\]/\\\]/g; # ]
    $result =~ s/\(/\\\(/g; # (
    $result =~ s/\)/\\\)/g; # )
    $result =~ s/\./\\\./g; # .
    $result =~ s/\|/\\\|/g; # |
    $result =~ s/\{/\\\{/g; # {
    $result =~ s/\}/\\\}/g; # }
    $result =~ s/\*/\\\*/g; # *  
    $result =~ s/\+/\\\+/g; # +
    $result =~ s/\?/\\\?/g; # ?
    $result =~ s/\^/\\\^/g; # ^
    $result =~ s/\$/\\\$/g; # $

    return $result;
}

# transform a string into an xml-safe copy
sub xmlsafe {
    my $str = $_[0];
    $str =~ s/</&lt;/g;
    $str =~ s/>/&gt;/g;
    $str =~ s/&/&\#38;/g;

    return $str;
}


sub is_ignored {
    my ($str, $array) = @_;
    my @ign = @$array;
    
    foreach my $line (@ign) {
	chomp($line);
	if ($str =~ m/$line/i) {
	    return 1;
	}
    }

    return 0;
}



my ($dir, $referency,@args) = @ARGV;
my $cmd = join " ", @args;
$cmd =~ s/&lt;/</g;
$cmd =~ s/&gt;/>/g;


#my $tmpfile = `mktemp /tmp/outcmp.XXXXXXXXXXXXX`;
#print "tmpfile is".$tmpfile."\n";

chdir($dir);

my $out = "STDOUT:\n";
my $err = "STDERR:\n";
my $start = time();

open (CMD, 
      "($cmd | sed 's/^/STDOUT:/') 2>&1 |");

while (<CMD>) {
    if (s/^STDOUT://)  {
	$out = $out.$_;
    } else {
	$err = $err.$_;
    }
}


#my $out = `$cmd`;
my $end = time();
my $length = $end-$start;

my $code = $?>>8;
my $strcode = "CODE:\n".$code;

my $shot = $strcode."\n".$out."\n".$err;

open(SHOT, "> shot");
print SHOT $shot;
close(SHOT);

if (!(-e $referency)) {
    my @path = split(/\//, $referency);
    my $dir = "";
    for (my $i = 0; $i < $#path; $i++) {
	$dir = $dir.$path[$i]."/";
	if (!(-e $dir)) {
	    mkdir($dir, 0755);
	}
	}
    open (FDD, "> $referency");
    print FDD $shot;
    close(FDD);
}

open (FD, $referency);
my @ref = <FD>;
close (FD);

open (IGN, "/home1/codmon/codmon/wrappers/ignore");
my @ign = <IGN>;
close (IGN);

#chomp($out);
#my @actual = split (/\n/, $out);
my $diffs = 0;

my $tgt = $shot;
$tgt =~ s/\n//g;
my @para = ();

for (my $i = 0; $i <= $#ref; $i++) {
    chomp $ref[$i];
    if ($ref[$i] =~ m/^\/\//) {         # Unmatched parts
#	    print "Comment\n";
	next; 
    } 
    if ($ref[$i] =~ m/(.*?)\/\//) {     # remove endline comments
	$ref[$i] = $1;
    }
    if ($ref[$i] eq "{") {               # Start of a parallel statement
#	    print "Parallel\n";
	$i++;
	chomp $ref[$i];
	while (!($ref[$i] eq "}")) {
	    # print $ref[$i];
	    if (is_ignored($ref[$i], \@ign) == 0) {
		push (@para, protect($ref[$i]));
	    }
	    $i++;
	    chomp $ref[$i];
	}
#	     print "Fin Parallel\n";
	#   $i++;
    }
    if ($#para < 0) {                   # No para statement in progress, read a normal line
#	    print "Seq\n";
	if (is_ignored($ref[$i], \@ign) == 0) {
	    push (@para, protect($ref[$i]));
	}
    }
    
    my $low = length($out);
    for (my $j = 0; $j <= $#para; $j++) {
#	    print "Action $para[$j] in $tgt\n";
	if ($tgt =~ m/(.*?)($para[$j])/) {
#		print "Match !\n";
	    if ($low > length($1)) { $low = length($1); }
	    $tgt = substr($tgt, 0, length($1)).substr($tgt, length($1)+length($2));
	    splice(@para, $j, 1);
	    $j = -1;
	}
    }
    $out = substr($out, $low); 

    if ($#para >= 0) { # force break out of the loop
	$i = $#ref;
    }
    
#       if ($#para < 0) {
#	    print STDERR "Algo unmatch: <br/>\n";
#	    print STDERR join "<br/>\n", @para;
#	    print STDERR "\nOut is \n";
#	    print STDERR $out."\n";
#	}

}

if ($#para < 0) {
    exit(0);
} else {
    print STDERR "<html>Unmatched: <br/>\n<font color=\"red\">";
    foreach my $line (@para) {
	$line =~ s/\\(.)/$1/g;
	print STDERR xmlsafe($line)."<br/>\n";
    }
    print STDERR "</font>";
    print STDERR "<br/>\n<table width=\"100%\" border=\"1\"><tr bgcolor=\"#E5E5E5\"><td width=\"50%\" align=\"center\">Referency</td><td width=\"50%\" align=\"center\">Actual</td></tr>\n";
    print STDERR "<tr valign=\"top\"><td>\n";
    foreach my $line (@ref) {
	if ((is_in $line, \@para)==1) { print STDERR "<font color=\"red\">"; } else { print STDERR "<font>"; }
	while (length($line) > 80) {
	    my $tmp = substr($line,0,80);
	    print STDERR xmlsafe($tmp)."<br/>\n";
	    $line = substr($line, 80);
	}
	print STDERR xmlsafe($line)."</font><br/>\n";
    }
    print STDERR "</td><td>\n";
    foreach my $line (split(/\n/, $shot)) {
	if ((is_in $line, \@para)==1) { print STDERR "<font color=\"red\">"; } else { print STDERR "<font>"; }
	while (length($line) > 80) {
	    my $tmp = substr($line,0,80);
	    print STDERR xmlsafe($tmp)."<br/>\n";
	    $line = substr($line, 80);
	}
	print STDERR xmlsafe($line)."</font><br/>\n";
    }
    print STDERR "</td></tr></table>\n</html>";
    exit(1);
}



#    print "<test id=\"time\" name=\"Time\" value=\"$length\" unit=\"s\"/>\n";

exit($diffs);



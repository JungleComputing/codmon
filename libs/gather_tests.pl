#!/usr/bin/perl

# Usage : perl gather_tests.pl <baseConfig> <ibis.implementation> <basedir>+

#use strict;

if (@ARGV < 3) {
    print STDERR "Wrong number of arguments. Usage:\nperl gather_tests.pl <sensors> <ibis.implementation> <basedir>+\n";
    exit 1;
}

$ibis = $ARGV[1];
my $output = "$ARGV[0]-$ibis.xml";
open (OUTPUT, "$ARGV[0].xml") or die "You must specify an existing baseConfig !";
my @source = <OUTPUT>;
close OUTPUT;

my $codmonhome = $ENV{'CODMON_HOME'};

open (IGNOREDIRS, "$codmonhome/codmon/local/ignoredirs") or die "Couldn't find the ignore file";
my @ignoredirs = <IGNOREDIRS>;
close (IGNOREDIRS);
foreach my $ignored (@ignoredirs) {
    chomp $ignored;
}

open (OUTPUT, "> $output");

my $index = 0;
for($index = 0; $index <= $#source; $index++) {
    if ($source[$index] eq "<!-- Ibis functionalities -->\n") {
	last;
    }
    $source[$index] =~ s/\$\{ibis.implementation\}/$ibis/g;
    print OUTPUT $source[$index];
}

print OUTPUT "<!-- Ibis functionalities -->\n\n\n";

for ($i = 2; $i < @ARGV; $i++) {
    loopDir($ARGV[$i]);
}


print OUTPUT "\n\n\n<!-- /Ibis functionalities -->\n";

for(; $index <= $#source; $index++) {
    if ($source[$index] eq "<!-- /Ibis functionalities -->\n") {
	last;
    }
}

for($index++; $index <= $#source; $index++) {
    $source[$index] =~ s/\$\{ibis.implementation\}/$ibis/g;
    print OUTPUT $source[$index];
}

close(OUTPUT);


sub loopDir {
   local($dir) = @_;
#   print "Dir $dir\n";
   local(*DIR);
   opendir(DIR, $dir) || die "Cannot open $dir\n";
   while ($f=readdir(DIR)) {
      next if ($f eq "." || $f eq "..");

      my $newdir = "$dir/$f";
      my $is_ignored = 0;
      foreach my $ignored (@ignoredirs) {
	  if ($newdir =~ m/$ignored$/) {
	      $is_ignored = 1;
	  }
      }
      if ($is_ignored == 1) { next; }
#      print "Analyzing $f\n";
      if (-d $newdir) {
         &loopDir($newdir);
      } elsif ($f eq ".codmon") {
#	  print "Codmon\n";
	  my $location = $newdir;
	  my $wrapper = "outcmp_ibis";
	  if (-e "$dir/.codmon_wrapper") {
	      $wrapper = `cat $dir/.codmon_wrapper`;
	  }

	  $location =~ m{$codmonhome/(.*)/\.codmon};
	  $location = $1;
#	  $location =~ s/\//_/g;
#	  print $location."\n";
	  open (FILE, $newdir);
	  foreach my $line (<FILE>) {
	      chomp($line);
	      if ($line =~ m/^\#/) { next; }
	      if ($line =~ m/^\/\//) {
		  $line = substr($line, 2);
		  my $id = $location."_".$line;
		  $id =~ s/[ -\.\`\/]/_/g;
		  $id =~ s/__/_/g;
		  $id =~ s/__/_/g;
		  $id =~ s/_$//;
		  my $nbproc = $line;
		  $nbproc =~ s/^([0-9])*\s.*$/$1/;
		  
		  my $cmdargs = $line;
		  $cmdargs =~ s/^[0-9]*\s(.*)$/$1/;

#	      print $id."\n";
		  print OUTPUT "<sensor id=\"$id\" name=\"$location $line\" wrapper=\"$wrapper\" cmd=\"$dir $nbproc -Dibis.implementation=$ibis $cmdargs\" scope=\"ibis/src\" scheduled=\"true\" graph=\"false\" enabled=\"false\" />\n";
	      }
	      elsif ($line =~ m/^(.*?){(.*)}$/) {
		  $line = $1;
		  my @excludes = split(/ /,$2);
		  my $is_excluded = 0;
		  foreach my $exclude (@excludes) {
		      if ($exclude eq $ibis) {
			  $is_excluded = 1;
		      }
		  }

		  my $id = $location."_".$line;
		  $id =~ s/[ -\.\`\/]/_/g;
		  $id =~ s/__/_/g;
		  $id =~ s/__/_/g;
		  $id =~ s/_$//;
		  my $nbproc = $line;
		  $nbproc =~ s/^([0-9])*\s.*$/$1/;
		  
		  my $cmdargs = $line;
		  $cmdargs =~ s/^[0-9]*\s(.*)$/$1/;

#	      print $id."\n";
		  if ($is_excluded == 1) { 
		      print OUTPUT "<sensor id=\"$id\" name=\"$location $line\" wrapper=\"$wrapper\" cmd=\"$dir $nbproc -Dibis.implementation=$ibis $cmdargs\" scope=\"ibis/src\" scheduled=\"true\" graph=\"false\" enabled=\"false\" />\n";
		  } else {
		      print OUTPUT "<sensor id=\"$id\" name=\"$location $line\" wrapper=\"$wrapper\" cmd=\"$dir $nbproc -Dibis.implementation=$ibis $cmdargs\" scope=\"ibis/src\" scheduled=\"true\" graph=\"false\"/>\n";
		  }
	      }
	      else {
		  my $id = $location."_".$line;
		  $id =~ s/[ -\.\`\/]/_/g;
		  $id =~ s/__/_/g;
		  $id =~ s/__/_/g;
		  $id =~ s/_$//;
		  my $nbproc = $line;
		  $nbproc =~ s/^([0-9])*\s.*$/$1/;
		  
		  my $cmdargs = $line;
		  $cmdargs =~ s/^[0-9]*\s(.*)$/$1/;

#	      print $id."\n";
		  print OUTPUT "<sensor id=\"$id\" name=\"$location $line\" wrapper=\"$wrapper\" cmd=\"$dir $nbproc -Dibis.implementation=$ibis $cmdargs\" scope=\"ibis/src\" scheduled=\"true\" graph=\"false\"/>\n";
	      }
	  }
	  close (FILE);
      }
  }
   closedir(DIR);
}




# <sensor id="satin_mtdf_par_prealloc" name="satin/mtdf/par_prealloc" wrapper="$wrapper" cmd="satin/mtdf/par_prealloc 2 Mtdf" scope="ibis/src" scheduled="true" graph="false" />
